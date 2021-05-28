package main

import (
	"bufio"
	"bytes"
	"strings"
)

type Stomp struct {
	headers map[string]string
	command string
	content []byte
}

func NewStomp() (stomp *Stomp) {
	stomp = &Stomp{}
	stomp.headers = make(map[string]string)
	return
}

func (stomp *Stomp) Write(out *bytes.Buffer) {
	out.Write([]byte(stomp.command))
	out.Write([]byte("\n"))
	for key, value := range stomp.headers {
		out.Write([]byte(key))
		out.Write([]byte(":"))
		out.Write([]byte(value))
		out.Write([]byte("\n"))
	}
	out.Write([]byte("\n"))
	if stomp.content != nil {
		out.Write(stomp.content)
	}
	out.Write([]byte{0})
}

func (stomp *Stomp) Parse(reader *bytes.Reader) error {
	buf := bufio.NewReader(reader)
	line, _, err := buf.ReadLine()
	if err != nil {
		return err
	}
	stomp.command = string(line)
	for {
		lineBytes, _, err := buf.ReadLine()
		if err != nil {
			return err
		}
		line := string(lineBytes)
		if line == "" {
			break
		}
		arr := strings.SplitN(line, ":", 2)
		stomp.headers[arr[0]] = arr[1]
	}
	b, err := buf.ReadBytes(0)
	if err != nil {
		return err
	}
	// strip ending zero byte
	stomp.content = b[:len(b)-1]
	return nil
}
