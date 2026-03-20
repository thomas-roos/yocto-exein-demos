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
  qemu-ex machine/qemux86-64 distro/poky core/yocto/sstate-mirror-cdn --non-interactive && \
  cd -
```

**qemu-ex (arm64):**
```bash
cd bitbake/bin/ && \
./bitbake-setup --setting default top-dir-prefix $PWD/../../ init \
  $PWD/../../bitbake-setup.conf.json \
  qemu-ex machine/qemuarm64 distro/poky core/yocto/sstate-mirror-cdn --non-interactive && \
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

> The build directories will be created at `../bitbake-builds/` relative to the repo.

3. Install buildtools (required once per build directory):

```bash
. ../bitbake-builds/<build-dir>/build/init-build-env && bitbake-setup install-buildtools
```

4. Source the build environment:

```bash
. ../bitbake-builds/<build-dir>/buildtools/environment-setup-x86_64-pokysdk-linux && \
. ../bitbake-builds/<build-dir>/build/init-build-env
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

**qemu-ex:**
```bash
runqemu qemux86-64 qemu-ex-image nographic
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
