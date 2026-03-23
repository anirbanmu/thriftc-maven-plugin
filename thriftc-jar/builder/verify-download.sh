#!/usr/bin/env bash
set -euo pipefail

# verifies a downloaded apache thrift artifact against its sha256 + gpg signature.
# usage: verify-download.sh <keys-file> <artifact-path> <sha256-url> <asc-url>

if [ $# -ne 4 ]; then
    echo "usage: $0 <keys-file> <artifact-path> <sha256-url> <asc-url>" >&2
    exit 1
fi

KEYS_FILE="$1"
ARTIFACT="$2"
SHA256_URL="$3"
ASC_URL="$4"

TMPDIR="$(mktemp -d)"
trap 'rm -rf "$TMPDIR"' EXIT

curl -fSL "$SHA256_URL" -o "$TMPDIR/checksum.sha256"
curl -fSL "$ASC_URL" -o "$TMPDIR/signature.asc"

# sha256 -- corruption check.
# portable: macOS has shasum, linux has sha256sum.
if command -v sha256sum &>/dev/null; then
    sha256() { sha256sum "$1" | cut -d' ' -f1; }
else
    sha256() { shasum -a 256 "$1" | cut -d' ' -f1; }
fi

EXPECTED=$(cut -d' ' -f1 "$TMPDIR/checksum.sha256")
ACTUAL=$(sha256 "$ARTIFACT")
if [ "$EXPECTED" != "$ACTUAL" ]; then
    echo "SHA256 mismatch: expected=$EXPECTED actual=$ACTUAL" >&2
    exit 1
fi
echo "SHA256 OK"

# gpg -- authenticity check against pinned apache thrift KEYS.
# isolated keyring so we don't pollute the user's gpg state.
export GNUPGHOME="$TMPDIR/gnupg"
mkdir -p "$GNUPGHOME"
chmod 700 "$GNUPGHOME"
gpg --batch --quiet --import "$KEYS_FILE"
gpg --batch --verify "$TMPDIR/signature.asc" "$ARTIFACT"
echo "GPG signature OK"
