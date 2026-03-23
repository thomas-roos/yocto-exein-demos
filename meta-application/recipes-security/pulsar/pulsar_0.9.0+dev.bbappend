FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://0001-bpf-builder-suppress-Wdefault-const-init-var-unsafe.patch \
            file://pulsar.ini \
            file://pulsard.service \
            "

inherit systemd

SYSTEMD_SERVICE:${PN} = "pulsard.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install:append() {
    install -m 644 ${UNPACKDIR}/pulsar.ini ${D}/var/lib/pulsar/pulsar.ini
    install -d ${D}${systemd_system_unitdir}
    install -m 644 ${UNPACKDIR}/pulsard.service ${D}${systemd_system_unitdir}/pulsard.service
}
