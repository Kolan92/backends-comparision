# Backends comparison

Goal for this repo is to compare set of backends written in different languages.

## Testing method

The [vegeta](https://github.com/tsenart/vegeta) http load tool is used for testing the repository.

Simple run for the golang backend: 

```bash
echo "GET http://localhost:8080/" | vegeta attack -duration=60s | tee golang_results.bin | vegeta report
  vegeta report -type=json golang_results.bin > golang_metrics.json
  cat golang_results.bin | vegeta plot > golang_plot.html
  cat golang_results.bin | vegeta report -type="hist[0,100ms,200ms,300ms]"
```