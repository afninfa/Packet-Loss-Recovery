JAVAC = javac
JAVA  = java

SRC = $(wildcard *.java)
CLASSES = $(SRC:.java=.class)

%.class: %.java
	$(JAVAC) -d build $*.java

run-client: Client.class
	$(JAVA) -cp build Client

run-server: Server.class
	$(JAVA) -cp build Server

.PHONY: all
all: clean $(CLASSES)

.PHONY: clean
clean:
	rm -rf build
