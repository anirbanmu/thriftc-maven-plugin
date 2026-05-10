# ubuntu 20.04 targets glibc 2.31 for broad linux compatibility (covers RHEL 9, Debian 11+).
# -static-libstdc++ -static-libgcc ensures the binary only dynamically links libc.
FROM ubuntu:20.04 AS builder

ARG THRIFT_VERSION=0.23.0
ARG BISON_VERSION=3.8.2
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    flex \
    libboost-dev \
    libssl-dev \
    curl \
    tar \
    ca-certificates \
    gnupg \
    m4 \
    && rm -rf /var/lib/apt/lists/*

# pinned signing keys (trust anchors checked into the repo)
COPY KEYS /tmp/KEYS
COPY BISON-KEYS /tmp/BISON-KEYS
RUN gpg --import /tmp/KEYS /tmp/BISON-KEYS

# thrift 0.23.0+ needs bison >= 3.7 (--file-prefix-map); ubuntu 20.04 ships 3.5.1.
# build bison from source, verified against pinned GPG key.
RUN cd /tmp \
    && curl -fSL "https://ftp.gnu.org/gnu/bison/bison-${BISON_VERSION}.tar.gz" -O \
    && curl -fSL "https://ftp.gnu.org/gnu/bison/bison-${BISON_VERSION}.tar.gz.sig" -O \
    && gpg --verify "bison-${BISON_VERSION}.tar.gz.sig" "bison-${BISON_VERSION}.tar.gz" \
    && tar -xzf "bison-${BISON_VERSION}.tar.gz" \
    && cd "bison-${BISON_VERSION}" \
    && ./configure --prefix=/usr/local --quiet \
    && make -j"$(nproc)" \
    && make install \
    && cd /tmp && rm -rf "bison-${BISON_VERSION}" "bison-${BISON_VERSION}.tar.gz" "bison-${BISON_VERSION}.tar.gz.sig"

RUN cd /tmp \
    && curl -fSL "https://dlcdn.apache.org/thrift/${THRIFT_VERSION}/thrift-${THRIFT_VERSION}.tar.gz" -O \
    && curl -fSL "https://dlcdn.apache.org/thrift/${THRIFT_VERSION}/thrift-${THRIFT_VERSION}.tar.gz.sha256" -O \
    && curl -fSL "https://dlcdn.apache.org/thrift/${THRIFT_VERSION}/thrift-${THRIFT_VERSION}.tar.gz.asc" -O \
    && sha256sum -c "thrift-${THRIFT_VERSION}.tar.gz.sha256" \
    && gpg --verify "thrift-${THRIFT_VERSION}.tar.gz.asc" "thrift-${THRIFT_VERSION}.tar.gz" \
    && tar -xzf "thrift-${THRIFT_VERSION}.tar.gz" \
    && rm "thrift-${THRIFT_VERSION}.tar.gz" "thrift-${THRIFT_VERSION}.tar.gz.sha256" "thrift-${THRIFT_VERSION}.tar.gz.asc"

WORKDIR /tmp/thrift-${THRIFT_VERSION}

RUN cmake \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
    -DCMAKE_CXX_FLAGS="-static-libstdc++ -static-libgcc" \
    -DCMAKE_C_FLAGS="-static-libgcc" \
    -DBUILD_COMPILER=ON \
    -DBUILD_LIBRARIES=OFF \
    -DBUILD_TESTING=OFF \
    -DBUILD_TUTORIALS=OFF \
    .

RUN make -j"$(nproc)" thrift-compiler

# fixed path so the scratch stage doesn't need the version ARG
RUN cp compiler/cpp/bin/thrift /thriftc && strip /thriftc

FROM scratch
COPY --from=builder /thriftc /thriftc
