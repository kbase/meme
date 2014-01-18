TOP_DIR = ../..
include $(TOP_DIR)/tools/Makefile.common
KB_RUNTIME ?= /kb/runtime
DEPLOY_RUNTIME ?= $(KB_RUNTIME)
KB_TOP ?= /kb/deployment
TARGET ?= $(KB_TOP)
CURR_DIR = $(shell pwd)
TARGET_DIR = $(TARGET)/services/$(SERVICE_NAME)
WORK_DIR = /var/tmp/meme
TARGET_PORT = 7077
THREADPOOL_SIZE = 20
SERVICE_NAME = $(shell basename $(CURR_DIR))
SERVICE_SPEC = ./MEME.spec
SERVICE_PORT = $(TARGET_PORT)
SERVICE_DIR = $(TARGET_DIR)
SERVLET_CLASS = us.kbase.meme.MEMEServer
MAIN_CLASS = us.kbase.meme.MemeServerInvoker
SERVICE_PSGI = $(SERVICE_NAME).psgi
TPAGE_ARGS = --define kb_top=$(TARGET) --define kb_runtime=$(DEPLOY_RUNTIME) --define kb_service_name=$(SERVICE_NAME) --define kb_service_dir=$(SERVICE_DIR) --define kb_service_port=$(SERVICE_PORT) --define kb_psgi=$(SERVICE_PSGI)
JOB_DIR = /var/tmp/meme
DEPLOY_JAR = $(KB_TOP)/lib/jars/meme
DEPLOY_CLUSTER = /kb/deployment/meme
	
default: compile

deploy: distrib deploy-client deploy-jar

deploy-jar: compile-jar deploy-sh-scripts distrib-jar

compile-jar: src lib
	./make_jar.sh $(MAIN_CLASS)

distrib-jar:
	export KB_TOP=$(TARGET)
	rm -rf $(DEPLOY_JAR)
	mkdir -p $(DEPLOY_JAR)/lib
	cp ./lib/*.jar $(DEPLOY_JAR)/lib
	cp ./dist/meme.jar $(DEPLOY_JAR)

deploy-all: distrib deploy-client

deploy-client: deploy-libs deploy-scripts deploy-docs

deploy-libs: build-libs
	rsync --exclude '*.bak*' -arv lib/. $(TARGET)/lib/.

deploy-scripts:
	export KB_TOP=$(TARGET); \
	export KB_RUNTIME=$(DEPLOY_RUNTIME); \
	export KB_PERL_PATH=$(TARGET)/lib bash ; \
	for src in $(SRC_PERL) ; do \
		basefile=`basename $$src`; \
		base=`basename $$src .pl`; \
		echo install $$src $$base ; \
		cp $$src $(TARGET)/plbin ; \
		$(WRAP_PERL_SCRIPT) "$(TARGET)/plbin/$$basefile" $(TARGET)/bin/$$base ; \
	done

deploy-docs: build-docs
	-mkdir -p $(TARGET)/services/$(SERVICE_NAME)/webroot/.
	cp docs/*.html $(TARGET)/services/$(SERVICE_NAME)/webroot/.

build-docs: compile-docs
	pod2html --infile=lib/Bio/KBase/$(SERVICE_NAME)/Client.pm --outfile=docs/$(SERVICE_NAME).html

compile-docs: build-libs

build-libs:
	compile_typespec \
		--psgi $(SERVICE_PSGI)  \
		--impl Bio::KBase::$(SERVICE_NAME)::$(SERVICE_NAME)Impl \
		--service Bio::KBase::$(SERVICE_NAME)::Service \
		--client Bio::KBase::$(SERVICE_NAME)::Client \
		--py biokbase/$(SERVICE_NAME)/Client \
		--js javascript/$(SERVICE_NAME)/Client \
		$(SERVICE_SPEC) lib

SRC_SH = $(wildcard scripts/*.sh)
WRAP_SH_TOOL = wrap_sh
WRAP_SH_SCRIPT = bash $(TOOLS_DIR)/$(WRAP_SH_TOOL).sh

deploy-sh-scripts:
	mkdir -p $(TARGET)/shbin; \
	export KB_TOP=$(TARGET); \
	export KB_RUNTIME=$(DEPLOY_RUNTIME); \
	for src in $(SRC_SH) ; do \
		basefile=`basename $$src`; \
		base=`basename $$src .sh`; \
		echo install $$src $$base ; \
		cp $$src $(TARGET)/shbin ; \
		$(WRAP_SH_SCRIPT) "$(TARGET)/shbin/$$basefile" $(TARGET)/bin/$$base ; \
	done 

compile: src lib
	./make_war.sh $(SERVLET_CLASS)

distrib:
	@echo "Target folder: $(TARGET_DIR)"
	test -d $(WORK_DIR) || mkdir -p $(WORK_DIR)
	mkdir -p $(TARGET_DIR)
	cp -f ./dist/service.war $(TARGET_DIR)
	cp -f ./glassfish_start_service.sh $(TARGET_DIR)
	cp -f ./glassfish_stop_service.sh $(TARGET_DIR)
	cp -f ./meme.awf $(TARGET_DIR)
	echo "./glassfish_start_service.sh $(TARGET_DIR)/service.war $(TARGET_PORT) $(THREADPOOL_SIZE)" > $(TARGET_DIR)/start_service.sh
	chmod +x $(TARGET_DIR)/start_service.sh
	echo "./glassfish_stop_service.sh $(TARGET_PORT)" > $(TARGET_DIR)/stop_service.sh
	chmod +x $(TARGET_DIR)/stop_service.sh

test: test-scripts test-jar
	@echo "running script tests"

test-scripts:
	# run each test
	$(DEPLOY_RUNTIME)/bin/perl test/script_tests-command-line.t ; \
	if [ $$? -ne 0 ] ; then \
		exit 1 ; \
	fi \

test-jar:
	# run each test
	$(DEPLOY_RUNTIME)/bin/perl test/test_meme_server_invoker.t ; \
	if [ $$? -ne 0 ] ; then \
		exit 1 ; \
	fi \

clean:
	@echo "nothing to clean"
	
include $(TOP_DIR)/tools/Makefile.common.rules	
