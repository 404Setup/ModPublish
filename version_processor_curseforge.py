#!/usr/bin/env python3
import json
import requests
from datetime import datetime
import os
import sys

def read_minecraft_version_json():
    """Read the minecraft.version.json file"""
    try:
        with open('minecraft.version.json', 'r', encoding='utf-8') as f:
            data = json.load(f)
        print(f"✅ Successfully read minecraft.version.json with {len(data)} versions")
        return data
    except FileNotFoundError:
        print("❌ Error: minecraft.version.json file not found")
        return None
    except json.JSONDecodeError as e:
        print(f"❌ JSON parsing error: {e}")
        return None
    except Exception as e:
        print(f"❌ Unknown error occurred while reading file: {e}")
        return None

def fetch_curseforge_versions():
    """Fetch Minecraft version data from CurseForge API"""
    url = "https://api.curseforge.com/v1/minecraft/version"

    try:
        print("Fetching version data from CurseForge API...")
        response = requests.get(url, timeout=30)
        response.raise_for_status()

        data = response.json()
        versions = data.get('data', [])

        print(f"✅ Successfully fetched CurseForge API data with {len(versions)} versions")
        return versions

    except requests.exceptions.RequestException as e:
        print(f"❌ Network request error: {e}")
        return None
    except json.JSONDecodeError as e:
        print(f"❌ JSON parsing error: {e}")
        return None
    except Exception as e:
        print(f"❌ Unknown error occurred while fetching CurseForge data: {e}")
        return None

def create_version_mapping(curseforge_versions):
    """Create mapping from version string to game version ID"""
    version_mapping = {}

    for version in curseforge_versions:
        version_string = version.get('versionString', '')
        game_version_id = version.get('gameVersionId', -1)

        if version_string:
            version_mapping[version_string] = game_version_id

    print(f"✅ Created version mapping table with {len(version_mapping)} mappings")
    return version_mapping

def update_minecraft_versions(minecraft_versions, version_mapping):
    """Update minecraft version data by adding game version IDs"""
    updated_versions = []
    matched_count = 0

    for version in minecraft_versions:
        version_string = version.get('v', '')

        # Create new version object while maintaining original field order
        updated_version = {
            'v': version.get('v', ''),
            't': version.get('t', ''),
            'i': version_mapping.get(version_string, -1),  # Add game version ID
            'd': version.get('d', '')
        }

        # If a matching version ID is found, increment count
        if version_mapping.get(version_string, -1) != -1:
            matched_count += 1
            print(f"✅ Matched version: {version_string} -> ID: {version_mapping[version_string]}")

        updated_versions.append(updated_version)

    print(f"✅ Version update completed: {matched_count}/{len(minecraft_versions)} versions found corresponding game version IDs")
    return updated_versions

def save_updated_versions(updated_versions, output_file='minecraft.version.json'):
    """Save updated version data"""
    try:
        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(updated_versions, f, ensure_ascii=False, indent=2)

        print(f"✅ Successfully saved updated data to {output_file}")
        return True

    except Exception as e:
        print(f"❌ Error occurred while saving file: {e}")
        return False

def show_preview(updated_versions, count=5):
    """Show preview of update results"""
    print(f"\n📊 Update results preview (first {count} items):")
    print("-" * 60)

    for i, version in enumerate(updated_versions[:count]):
        status = "✅ Matched" if version['i'] != -1 else "❌ Not matched"
        print(f"   {i+1}. {version['v']} ({version['t']}) - ID: {version['i']} - {status}")

    if len(updated_versions) > count:
        print(f"   ... {len(updated_versions) - count} more versions")

    # Statistics
    matched_count = sum(1 for v in updated_versions if v['i'] != -1)
    total_count = len(updated_versions)
    match_rate = (matched_count / total_count * 100) if total_count > 0 else 0

    print(f"\n📈 Matching Statistics:")
    print(f"   Total versions: {total_count}")
    print(f"   Matched versions: {matched_count}")
    print(f"   Unmatched versions: {total_count - matched_count}")
    print(f"   Match rate: {match_rate:.1f}%")

def main():
    print("🎮 Minecraft Version Data Processor - CurseForge API Edition")
    print("=" * 60)

    # 1. Read minecraft.version.json
    minecraft_versions = read_minecraft_version_json()
    if not minecraft_versions:
        print("❌ Unable to continue processing, exiting program")
        sys.exit(1)

    # 2. Fetch version data from CurseForge API
    curseforge_versions = fetch_curseforge_versions()
    if not curseforge_versions:
        print("❌ Unable to fetch CurseForge data, exiting program")
        sys.exit(1)

    # 3. Create version mapping
    version_mapping = create_version_mapping(curseforge_versions)

    # 4. Update Minecraft version data
    print("\nStarting to update version data...")
    updated_versions = update_minecraft_versions(minecraft_versions, version_mapping)

    # 5. Save updated data
    success = save_updated_versions(updated_versions)

    if success:
        # 6. Show results preview
        show_preview(updated_versions)
        print(f"\n✅ Processing completed! Data has been saved to minecraft.version.json")
    else:
        print("\n❌ Processing failed!")
        sys.exit(1)

if __name__ == "__main__":
    main()