FROM centos:7.6.1810

RUN yum -y install wget

RUN wget -O go.tgz "https://dl.google.com/go/go1.13.7.linux-amd64.tar.gz"; \
    echo 'b3dd4bd781a0271b33168e627f7f43886b4c5d1c794a4015abf34e99c6526ca3 *go.tgz' | sha256sum -c -; \
    tar -C /usr/local -xzf go.tgz; \
    rm go.tgz;

RUN yum -y install gcc
RUN yum -y install openssl
RUN yum -y install openssl-devel
RUN yum -y install git

ENV GOPATH /go
ENV PATH $GOPATH/bin:/usr/local/go/bin:$PATH

RUN mkdir -p "$GOPATH/src" "$GOPATH/bin" && chmod -R 777 "$GOPATH"

RUN go get github.com/gorilla/websocket
RUN go get github.com/spacemonkeygo/openssl

WORKDIR $GOPATH
