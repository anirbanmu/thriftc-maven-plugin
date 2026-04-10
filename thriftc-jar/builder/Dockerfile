# ubuntu 20.04 targets glibc 2.31 for broad linux compatibility.
# -static-libstdc++ -static-libgcc ensures the binary only dynamically links libc.
FROM ubuntu:20.04 AS builder

ARG THRIFT_VERSION=0.22.0
ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    bison \
    flex \
    libboost-dev \
    libssl-dev \
    curl \
    tar \
    ca-certificates \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# pinned copy of apache thrift release signing keys (trust anchor)
COPY KEYS /tmp/KEYS
RUN gpg --import /tmp/KEYS

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
