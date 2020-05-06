This is a Java multi-thread sychronization project

In part1, sychronized keyword is used to ensure correct implementation of traffic rules with no priority involved, which introduces the problem of busy-waiting

In part2, a Reentrack lock and conditional variables are used to avoid busy waiting situation. This also allows scheduling threads based on its priority
