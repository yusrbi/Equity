#/bin/bash!

mvn install:install-file -Dfile=/home/yusra/workspace/quantum-tagger/lib/stanford-corenlp-3.6.0.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp -Dversion=3.6.0 -Dpackaging jar
mvn install:install-file -Dfile=/home/yusra/workspace/quantum-tagger/lib/stanford-corenlp-3.6.0-models.jar -DgroupId=edu.stanford.nlp -DartifactId=stanford-corenlp -Dversion=3.6.0 -Dclassifier models  -Dpackaging jar
