import json
import os
import subprocess
import sys


def extract_sha1() -> str:
    try:
        out = subprocess.check_output(
            [
                "keytool",
                "-list",
                "-v",
                "-keystore",
                "debug.keystore",
                "-alias",
                "androiddebugkey",
                "-storepass",
                "android",
                "-keypass",
                "android",
            ],
            text=True,
        )
        for line in out.splitlines():
            stripped = line.strip()
            if stripped.startswith("SHA1:") or stripped.startswith("SHA-1:"):
                hex_part = stripped.split(":", 1)[1]
                return hex_part.replace(":", "").replace(" ", "").strip().lower()
    except Exception as exc:
        print(f"Warning: unable to read debug.keystore SHA1: {exc}")
    return ""


def patch_google_services() -> None:
    pkg = "com.aistudio.betzonex.krvmpx"
    gs_path = "app/google-services.json"

    if not os.path.exists(gs_path):
        print("::warning::app/google-services.json not found in repo or GOOGLE_SERVICES_JSON secret; writing CI-only OAuth placeholder.")
        os.makedirs("app/src/main/res/values", exist_ok=True)
        with open("app/src/main/res/values/ci_fallback_oauth.xml", "w", encoding="utf-8") as f:
            f.write(
                '<?xml version="1.0" encoding="utf-8"?>\n'
                "<resources>\n"
                '    <string name="default_web_client_id" translatable="false"></string>\n'
                "</resources>\n"
            )
        return

    with open(gs_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    clients = data.get("client", [])
    has_pkg = any(
        c.get("client_info", {}).get("android_client_info", {}).get("package_name") == pkg
        for c in clients
    )

    if not has_pkg and clients:
        template = clients[0]
        proj_num = data.get("project_info", {}).get("project_number", "")
        web_clients = [oc for oc in template.get("oauth_client", []) if oc.get("client_type") == 3]
        sha1_hex = extract_sha1()
        android_client = {
            "client_id": f"{proj_num}-betzonex.apps.googleusercontent.com",
            "client_type": 1,
            "android_info": {
                "package_name": pkg,
                "certificate_hash": sha1_hex,
            },
        }
        new_entry = {
            "client_info": {
                "mobilesdk_app_id": f"1:{proj_num}:android:cdd593f56fd5945ca02f41",
                "android_client_info": {"package_name": pkg},
            },
            "oauth_client": [android_client] + web_clients,
            "api_key": template.get("api_key", []),
            "services": template.get("services", {}),
        }
        clients.insert(0, new_entry)
        data["client"] = clients
        with open(gs_path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)
        print(f"Auto-registered {pkg} into {gs_path} using existing Firebase project config.")
    else:
        print(f"Verified {pkg} present in {gs_path}.")


if __name__ == "__main__":
    sys.exit(patch_google_services() or 0)
