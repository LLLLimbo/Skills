#!/usr/bin/env bash
set -euo pipefail

repo_url="https://github.com/TNG/ArchUnit-Examples.git"
default_dest="/tmp/archunit-examples"
dest_dir="$default_dest"
clean=false

while [ "$#" -gt 0 ]; do
  case "$1" in
    --dest)
      if [ "$#" -lt 2 ]; then
        echo "Missing value for --dest" >&2
        exit 2
      fi
      dest_dir="$2"
      shift 2
      ;;
    --clean)
      clean=true
      shift
      ;;
    -h|--help)
      echo "Usage: $0 [--dest path] [--clean]" >&2
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

if [ -e "$dest_dir" ]; then
  if [ "$dest_dir" = "/" ]; then
    echo "Refusing to remove /" >&2
    exit 1
  fi
  if [ "$dest_dir" = "$default_dest" ] || [ "$clean" = "true" ]; then
    rm -rf "$dest_dir"
  else
    echo "Destination already exists: $dest_dir (use --clean to overwrite)" >&2
    exit 1
  fi
fi

git clone --depth 1 "$repo_url" "$dest_dir"

echo "Cloned to: $dest_dir"
