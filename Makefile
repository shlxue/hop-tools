default: all

MVN=mvn
EXT_CFG=extensions.xml

# building
.PHONY: clr prune
clr:
	$(MVN) clean

prune:
	$(MVN) dependency:purge-local-repository -DsnapshotsOnly -DreResolve=false

# checkstyle
.PHONY: fmt

fmt:
	mvn spotless:apply

.PHONY: bt
bt:
	mv .mvn/$(EXT_CFG) .mvn/tmp-$(EXT_CFG)
	$(MVN) install -pl build-tools
	mv .mvn/tmp-$(EXT_CFG) .mvn/$(EXT_CFG)

.PHONY: c b i t

c:
	$(MVN) compile
b:
	$(MVN) install -DinstalAtEnd
i:
	$(MVN) clean install
t:
	$(MVN) verify
sonar:
	$(MVN) sonar:sonar
rwt:
	$(MVN) clean test-compile -Dxwt.type=rwt

all: bt fmt i rwt
