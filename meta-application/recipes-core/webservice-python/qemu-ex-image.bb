SUMMARY = "QEMU image with Exein runtime and Python echo webservice"
DESCRIPTION = "Minimal QEMU-bootable image for testing Exein runtime with a Python HTTP echo service"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

IMAGE_FSTYPES = "ext4.zst"
inherit core-image

IMAGE_FEATURES += "empty-root-password allow-root-login"
IMAGE_LINGUAS = ""

IMAGE_INSTALL += " \
    dropbear \
    python3-core \
    python3-modules \
    webservice-python \
    pulsar \
    lsof \
    curl \
    jq \
"
