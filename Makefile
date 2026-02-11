VERSION := 3.5.2
NPM_VERSION := $(VERSION)

default:
	@echo "Use mvn directly or read the README.md file"
	@echo "Common commands:"
	@echo "  mvn compile        - Compile the project"
	@echo "  mvn test           - Run tests"
	@echo "  mvn package        - Build uber-JAR"
	@echo "  mvn clean          - Clean build artifacts"

# Build the uberjar only (no tests, faster for development)
build:
	mvn clean package -DskipTests

# Test the project
test:
	mvn test

# Prepare npm package directory with JAR file
npm-prepare: build
	@echo "Preparing npm package..."
	mkdir -p pkg/npm/lib
	cp target/mirthsync-$(VERSION)-standalone.jar pkg/npm/lib/mirthsync.jar
	@echo "Updating npm package version to $(NPM_VERSION)..."
	cd pkg/npm && npm version $(NPM_VERSION) --no-git-tag-version --allow-same-version
	@echo "npm package prepared in pkg/npm/"

# Build npm package tarball for local testing
npm-pack: npm-prepare
	@echo "Creating npm package tarball..."
	cd pkg/npm && npm pack
	mv pkg/npm/*.tgz target/
	@echo "Package created: target/saga-it-mirthsync-$(NPM_VERSION).tgz"

# Test npm package locally (install globally from tarball)
npm-test-install: npm-pack
	@echo "Installing npm package globally for testing..."
	npm install -g target/saga-it-mirthsync-$(NPM_VERSION).tgz
	@echo "Testing mirthsync command..."
	mirthsync -h

# Publish to npm (requires npm login)
npm-publish: npm-prepare
	@echo "Publishing to npm..."
	cd pkg/npm && npm publish --access public
	@echo "Published @saga-it/mirthsync@$(NPM_VERSION) to npm"

# Clean npm artifacts
npm-clean:
	rm -rf pkg/npm/lib/mirthsync.jar
	rm -f pkg/npm/*.tgz
	rm -f target/*.tgz

release:
	sed -E -i.bak "s/<version>[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?<\\/version>/<version>$(VERSION)<\\/version>/g" pom.xml
	rm -f pom.xml.bak
	sed -E -i.bak "s/(version of mirthSync is) \"[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?\"/\\1 \"$(VERSION)\"/g" README.md
	rm -f README.md.bak
	sed -E -i.bak "s/[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?/$(VERSION)/g" pkg/mirthsync.sh pkg/mirthsync.bat
	rm -f pkg/mirthsync.sh.bak pkg/mirthsync.bat.bak
	mvn clean test package
	mkdir -p target/mirthsync-$(VERSION)/lib
	cp -a pkg target/mirthsync-$(VERSION)/bin
	cp target/mirthsync-$(VERSION)-standalone.jar target/mirthsync-$(VERSION)/lib
	tar -C target/ -cvzf target/mirthsync-$(VERSION).tar.gz mirthsync-$(VERSION)
	cd target && zip -r mirthsync-$(VERSION).zip mirthsync-$(VERSION)
	gpg --detach-sign --armor target/mirthsync-$(VERSION).tar.gz
	gpg --detach-sign --armor target/mirthsync-$(VERSION).zip
	cd target && sha256sum mirthsync-$(VERSION).tar.gz mirthsync-$(VERSION).zip > CHECKSUMS.txt
	git add pom.xml README.md pkg/mirthsync.sh pkg/mirthsync.bat
	git commit -m "Release version $(VERSION)"
	git tag -a v$(VERSION) -m "Release $(VERSION)"

.PHONY: release default build test npm-prepare npm-pack npm-test-install npm-publish npm-clean
