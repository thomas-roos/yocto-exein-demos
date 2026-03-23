Yocto demo images for testing the Exein security runtime.

## Demos

| Name | Description | Target |
|---|---|---|
| `qemu-ex` | Minimal QEMU image with Exein/Pulsar security runtime and Python echo webservice | QEMU (x86-64, arm64) |
| `docker-ex` | Minimal OCI container image with Python echo webservice | Docker (x86-64) |


## Building with bitbake-setup

1. Clone with submodules:

```bash
git clone --recurse-submodules https://github.com/thomas-roos/yocto-exein-demos
```

Or if already cloned:

```bash
git submodule update --init --recursive
```

2. Initialize the build environment:

**qemu-ex (x86-64):**
```bash
cd bitbake/bin/ && \
./bitbake-setup --setting default top-dir-prefix $PWD/../../ init \
  $PWD/../../bitbake-setup.conf.json \
  qemu-ex machine/qemux86-64 distro/poky-altcfg core/yocto/sstate-mirror-cdn --non-interactive && \
  cd -
```

**qemu-ex (arm64):**
```bash
cd bitbake/bin/ && \
./bitbake-setup --setting default top-dir-prefix $PWD/../../ init \
  $PWD/../../bitbake-setup.conf.json \
  qemu-ex machine/qemuarm64 distro/poky-altcfg core/yocto/sstate-mirror-cdn --non-interactive && \
  cd -
```

**docker-ex:**
```bash
cd bitbake/bin/ && \
./bitbake-setup --setting default top-dir-prefix $PWD/../../ init \
  $PWD/../../bitbake-setup.conf.json \
  docker-ex machine/qemux86-64 distro/poky core/yocto/sstate-mirror-cdn --non-interactive && \
  cd -
```

3. Install buildtools (required once per build directory):

**qemu-ex (x86-64):**
```bash
. ./bitbake-builds/bitbake-setup-qemu-ex-distro_poky-altcfg-machine_qemux86-64/build/init-build-env && bitbake-setup install-buildtools
```

**qemu-ex (arm64):**
```bash
. ./bitbake-builds/bitbake-setup-qemu-ex-distro_poky-altcfg-machine_qemuarm64/build/init-build-env && bitbake-setup install-buildtools
```

**docker-ex:**
```bash
. ./bitbake-builds/bitbake-setup-docker-ex-distro_poky-machine_qemux86-64/build/init-build-env && bitbake-setup install-buildtools
```

4. Source the build environment:

**qemu-ex (x86-64):**
```bash
. ./bitbake-builds/bitbake-setup-qemu-ex-distro_poky-altcfg-machine_qemux86-64/build/init-build-env
```

**qemu-ex (arm64):**
```bash
. ./bitbake-builds/bitbake-setup-qemu-ex-distro_poky-altcfg-machine_qemuarm64/build/init-build-env
```

**docker-ex:**
```bash
. ./bitbake-builds/bitbake-setup-docker-ex-distro_poky-machine_qemux86-64/build/init-build-env
```

5. Build the image:

**qemu-ex:**
```bash
bitbake qemu-ex-image
```

**docker-ex:**
```bash
bitbake webservice-container
```

6. Run the image:

**qemu-ex** — add your user to the `kvm` group (required once):
```bash
sudo usermod -aG kvm $USER
```

Then run:
```bash
runqemu nographic snapshot kvm
```

Login as `root` with no password.

**docker-ex** — import and run the container:
```bash
docker import \
  --change 'ENTRYPOINT ["/usr/bin/echo-server"]' \
  --change 'CMD ["--host", "0.0.0.0", "--port", "8080"]' \
  --change 'EXPOSE 8080' \
  ./tmp/deploy/images/qemux86-64/webservice-container-qemux86-64.rootfs.tar.bz2 \
  webservice-container:latest
docker run -p 8080:8080 webservice-container:latest
```

7. Test the echo webservice:

```bash
curl -X POST -d "Hello Exein!" http://localhost:8080/test
```

## Testing Pulsar security runtime

### Additional terminals

SSH into the running VM from the host to open additional terminals:

```bash
ssh root@192.168.7.2
```

### Watch alerts in real time

Inside the QEMU VM, monitor Pulsar threat alerts:

```bash
journalctl -fu pulsard | uniq
```


To see all raw output including internal diagnostics:

```bash
journalctl -fu pulsard
```

### Trigger detections

In a separate terminal session inside the VM, run actions that Pulsar's built-in rules detect:

```bash
# Triggers "Read sensitive file" (severity: medium)
cat /etc/shadow
cat /etc/passwd
```

Each access will produce a JSON alert in the journal with the threat description,
severity, source file, and PID of the offending process.

### Alert format

Alerts are emitted as JSON objects. Key fields:

| Field | Description |
|---|---|
| `header.threat.description` | Human-readable rule name, e.g. "Read sensitive file" |
| `header.threat.extra.severity` | `low`, `medium`, `high`, or `critical` |
| `header.threat.extra.category` | MITRE ATT&CK tactic category |
| `header.pid` | PID of the process that triggered the rule |
| `header.image` | Executable path (empty if process started before Pulsar) |
| `payload.type` | Event type, e.g. `FileOpened`, `Exec` |
| `payload.content` | Event-specific data, e.g. `filename`, `flags` |

### Notes

- `image: ""` and `parent_pid: 0` in alerts mean the process was already running
  when Pulsar started and was not yet tracked. Detections are still accurate.
- `broadcast channel lagged N messages` warnings are benign and appear when many
  events arrive faster than the internal channel can drain (e.g. during a burst of
  file accesses).
- Pulsar in this version is **detect-only** — it logs threats but does not block or
  kill processes.
