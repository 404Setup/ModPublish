#!/usr/bin/env python3
import json
import sys
import requests


def fetch_minecraft_versions():
    """Fetch Minecraft version data from Mojang API"""
    url = "https://launchermeta.mojang.com/mc/game/version_manifest_v2.json"

    try:
        print("Fetching Minecraft version data from Mojang API...")
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

        print(f"✅ Successfully processed {len(processed_versions)} versions")
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


def fetch_curseforge_versions():
    """Fetch Minecraft version data from CurseForge API"""
    url = "https://api.curseforge.com/v1/minecraft/version"

    headers = {}

    try:
        print("\nFetching version data from CurseForge API...")
        response = requests.get(url, headers=headers, timeout=30)
        response.raise_for_status()

        data = response.json()
        versions = data.get('data', [])

        print(f"✅ Successfully got CurseForge API data with {len(versions)} versions")
        return versions

    except requests.exceptions.RequestException as e:
        print(f"❌ Network request error: {e}")
        return None
    except json.JSONDecodeError as e:
        print(f"❌ JSON parsing error: {e}")
        return None
    except Exception as e:
        print(f"❌ Unknown error getting CurseForge data: {e}")
        return None


def create_version_mapping(curseforge_versions):
    """Create mapping from version string to game version ID"""
    version_mapping = {}
    for version in curseforge_versions:
        version_string = version.get('versionString', '')
        game_version_id = version.get('gameVersionId', -1)
        if version_string:
            version_mapping[version_string] = game_version_id
    print(f"✅ Created version mapping table with {len(version_mapping)} entries")
    return version_mapping


def merge_curseforge_ids(minecraft_versions, version_mapping):
    """Update Minecraft version data by adding game version IDs"""
    updated_versions = []
    matched_count = 0
    print("\nMerging version data...")

    for version in minecraft_versions:
        version_string = version.get('v', '')
        game_version_id = version_mapping.get(version_string, -1)

        updated_version = {
            'v': version_string,
            't': version.get('t', ''),
            'i': game_version_id,
            'd': version.get('d', '')
        }

        if game_version_id != -1:
            matched_count += 1

        updated_versions.append(updated_version)

    print(f"✅ Version update complete: {matched_count}/{len(minecraft_versions)} versions found matching game version IDs")
    return updated_versions


def save_versions(versions, output_file='minecraft.version.json'):
    """Save final version data"""
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(versions, f, ensure_ascii=False, indent=2)
        print(f"✅ Successfully saved updated data to {output_file}")
        return True
    except Exception as e:
        print(f"❌ Error saving file: {e}")
        return False


def show_preview(updated_versions, count=5):
    """Show update result preview"""
    print(f"\n📊 Update Result Preview (First {count} items):")
    print("-" * 60)
    for i, version in enumerate(updated_versions[:count]):
        status = "✅ Matched" if version['i'] != -1 else "❌ Unmatched"
        print(f"   {i + 1}. {version['v']} ({version['t']}) - ID: {version['i']} - {status}")

    if len(updated_versions) > count:
        print(f"   ... {len(updated_versions) - count} more versions")

    matched_count = sum(1 for v in updated_versions if v['i'] != -1)
    total_count = len(updated_versions)
    match_rate = (matched_count / total_count * 100) if total_count > 0 else 0

    print(f"\n📈 Match Statistics:")
    print(f"   Total Versions: {total_count}")
    print(f"   Matched Versions: {matched_count}")
    print(f"   Unmatched Versions: {total_count - matched_count}")
    print(f"   Match Rate: {match_rate:.1f}%")


def main():
    print("🎮 Minecraft Version Data Processor")
    print("=" * 60)

    mojang_versions = fetch_minecraft_versions()
    if not mojang_versions:
        print("\n❌ Unable to get Mojang data, program exits")
        sys.exit(1)

    curseforge_versions = fetch_curseforge_versions()
    if not curseforge_versions:
        print("\n⚠️ Failed to get data from CurseForge, will only process Mojang data")
        version_mapping = {}
    else:
        version_mapping = create_version_mapping(curseforge_versions)

    merged_versions = merge_curseforge_ids(mojang_versions, version_mapping)

    success = save_versions(merged_versions)

    if success:
        show_preview(merged_versions)
        print(f"\n✅ Processing complete! Data saved to minecraft.version.json")
    else:
        print("\n❌ Processing failed!")
        sys.exit(1)


if __name__ == "__main__":
    main()