# World of lambda
- Here I added more and more usages about lambda.

- This is not a tutorial.

- This repo provides samples only.

- This code repo is in Kotlin just keep all simple and clean.

- Knowledge ranges from collection i.e foreach, stream to some high like [coroutines](https://github.com/Kotlin/kotlinx.coroutines/blob/master/coroutines-guide.md) .


### Foreach

1. Use JVM ```foreach``` to loop some outputs.

2. Use JVM ```stream().foreach``` to loop some outputs, Java8 Only.

3. Use a kind of map on ```stream``` to deal on data and output by ```foreach```.

4. Use ```sum``` to get summary of numbers.

5. Use ```parallel```, the order won't be ensured.

6. Use more than one streams to do task, i.e : ```map``` and ```flatMapToInt```  and make ```sum``` .

7. Use ```partitioningBy``` to generate partition collections from stream source.

8. Use inline function which is a feature of Kotlin to simulate ```foreach```.

### Coroutines

1. Basic usage, just call 2 functions that don't depend each other.

2. Basic usage, just call 2 functions that depend each other.

3. Basic usage of builder like *launch* *async* .

4. Combination of different context.

5. Cancel on job.

6. Join on job.

7. Ping-Pong to demo channel-fair.

8. Error(Exception)-handling.
