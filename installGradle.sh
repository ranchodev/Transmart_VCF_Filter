#!/bin/bash
curl -s "https://get.sdkman.io" | bash
echo "Y" > AnswerYes.txt
source $HOME/.sdkman/bin/sdkman-init.sh
sdk install gradle 4.6 < AnswerYes.txt