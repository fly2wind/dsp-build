FROM registry.suse.com/suse/sles12sp3:latest

ADD wget-1.16-11.1.x86_64.rpm .
ADD tar-1.27.1-14.3.x86_64.rpm .
RUN rpm -ivh wget-1.16-11.1.x86_64.rpm
RUN rpm -ivh tar-1.27.1-14.3.x86_64.rpm
RUN wget -q https://download.docker.com/linux/static/stable/x86_64/docker-20.10.3.tgz && tar xzvf docker-20.10.3.tgz --strip 1   -C /usr/bin
RUN mkdir -p ~/.docker/cli-plugins
ADD buildx-v0.9.1.linux-amd64 /root/.docker/cli-plugins/docker-buildx
RUN ls ~/.docker/cli-plugins
RUN chmod +x ~/.docker/cli-plugins/docker-buildx
RUN docker buildx version