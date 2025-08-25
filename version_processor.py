#!/usr/bin/env python3
import json
import re
import requests
from datetime import datetime

def fetch_minecraft_versions():
    url = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

    try:
        print("Fetching Minecraft version data...")
        response = requests.get(url, timeout=10)
        response.raise_for_status()

        data = response.json()

        processed_versions = []

        for version in data["versions"]:
            release_time = version["releaseTime"]
            if release_time.endswith("+00:00"):
                release_time = release_time[:-6] + "Z"

            processed_version = {
                "v": version["id"],
                "t": version["type"],
                "d": release_time
            }
            processed_versions.append(processed_version)

        output_file = "minecraft.version.json"
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(processed_versions, f, ensure_ascii=False, indent=2)

        print(f"✅ Successfully processed {len(processed_versions)} versions")
        print(f"✅ Data saved to {output_file}")

        latest_info = data["latest"]
        print(f"\n📋 Latest version info:")
        print(f"   Release: {latest_info['release']}")
        print(f"   Snapshot: {latest_info['snapshot']}")

        return processed_versions

    except requests.exceptions.RequestException as e:
        print(f"❌ Network request error: {e}")
        return None
    except json.JSONDecodeError as e:
        print(f"❌ JSON parsing error: {e}")
        return None
    except KeyError as e:
        print(f"❌ Data format error, missing field: {e}")
        return None
    except Exception as e:
        print(f"❌ Unknown error: {e}")
        return None


def main():
    print("🎮 Minecraft Version Manifest Processor")
    print("=" * 40)

    versions = fetch_minecraft_versions()

    if versions:
        print(f"\n📊 Processing result preview (first 5 items):")
        for i, version in enumerate(versions[:5]):
            print(f"   {i + 1}. {version['v']} ({version['t']}) - {version['d']}")

        if len(versions) > 5:
            print(f"   ... {len(versions) - 5} more versions")


if __name__ == "__main__":
    main()
