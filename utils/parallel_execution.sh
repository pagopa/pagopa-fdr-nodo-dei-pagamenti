NUM_EXEC=$1
NUM_PAYMENTS=$2
ENVIRONMENT=$3
SUBKEY=$4

start_time=$(date +%s)

for ((i=1; i<=NUM_EXEC; i++)); do
  sh nodoInviaFlussoRendicontazione.sh "$NUM_PAYMENTS" "$ENVIRONMENT" "$SUBKEY" > /dev/null 2>&1 &
done

wait

end_time=$(date +%s)
execution_time=$((end_time - start_time))

echo "Finish: $execution_time seconds"

rm *.xml