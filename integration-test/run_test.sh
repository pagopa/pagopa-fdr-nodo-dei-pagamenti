#!/bin/sh

if [ -z $ENV ]
then
  echo "DEV environment..."
  cp config/dev.json config/env.json
else
  echo "Setting $ENV environment..."
    cp config/${ENV}.json config/env.json
    sleep 1
fi

if [ -z $TAGS ]
then
  TAGS="runnable"
fi

if [ -z $JUNIT ]
then
  junit=""
else
  junit="--junit-directory=junit --junit"
fi

echo "Run test ..."
rm -rf results junit

behave --format allure_behave.formatter:AllureFormatter -o results $junit --tags=$TAGS --summary --show-timings -v

rm -rf results/history && cp -R reports/history results/history 2>/dev/null

allure generate results -o reports --clean
