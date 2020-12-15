#!/bin/bash
export XGENCLASSPATH=lib/w3c.jar:lib/xalan-2.0.0.jar:lib/xalanj1compat.jar:lib/xerces-1.2.3.jar:lib

mkdir build
mkdir build/crud
java -classpath $XGENCLASSPATH XtSax xml/$1.xml xsl/vo.xsl  > build/crud/$1VO.java
java -classpath $XGENCLASSPATH XtSax xml/$1.xml xsl/dao.xsl  > build/crud/$1DAO.java