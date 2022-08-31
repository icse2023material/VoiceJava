ls ./testcases/*.voiceJava |
while read file_name;
do
    cat "$file_name" >> all.voiceJava
    echo "" >> all.voiceJava
done

