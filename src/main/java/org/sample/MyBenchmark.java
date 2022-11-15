/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 50, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(5)
public class MyBenchmark {

    private int[] integersArray;

    @Benchmark
    public void sol2(Blackhole bh) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int elem : integersArray) {
            Integer count = counts.get(elem);
            if (count != null) {
                counts.put(elem, count + 1);
            } else {
                counts.put(elem, 1);
            }
        }
        int avg = 0;
        for (var elem : counts.keySet()) {
            avg += counts.get(elem) * elem;
        }
        avg = (int) Math.round((double) avg / integersArray.length);

        int result = 0;
        for (int key : counts.keySet()) {
            if (key != avg) {
                result += Math.abs(key - avg) * counts.get(key);
            }
        }
        bh.consume(result);
    }

    @Benchmark
    public void sol21(Blackhole bh) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int elem : integersArray) {
            counts.merge(elem, 1, Integer::sum);
        }
        int avg = 0;
        for (var elem : counts.keySet()) {
            avg += counts.get(elem) * elem;
        }
        avg = (int) Math.round((double) avg / integersArray.length);

        int result = 0;
        for (int key : counts.keySet()) {
            if (key != avg) {
                result += Math.abs(key - avg) * counts.get(key);
            }
        }
        bh.consume(result);
    }

    @Benchmark
    public void sol3(Blackhole bh) {
        int[] counts = new int[]{0, 0, 0, 0, 0};
        for (int elem : integersArray) {
            int count = counts[elem];
            counts[elem] = count + 1;
        }

        int avg = 0;
        for (int k = 1; k < 5; k++) {
            avg += counts[k] * k;
        }
        avg = Math.round((float) avg / integersArray.length);

        int result = 0;
        for (int k = 1; k < 5; k++) {
            if (k != avg) {
                result += Math.abs(k - avg) * counts[k];
            }
        }
        bh.consume(result);
    }

    @Benchmark
    public void sol4(Blackhole bh) {
        Map<Integer, Integer> map = Arrays.stream(integersArray).boxed()
                .collect(Collectors.toMap(Function.identity(), v -> 1, (e, i) -> e + 1));
        int average = Math.round((float) map.keySet().stream().mapToInt(k -> map.get(k) * k).sum() / integersArray.length);

        int result = map.keySet().stream()
                .mapToInt(i -> Math.abs(i - average) * map.get(i))
                .sum();
        bh.consume(result);
    }

    @Benchmark
    public void sol5(Blackhole bh) {
        int average = (int) (Math.round(Arrays.stream(integersArray).average().getAsDouble()));

        int result = Arrays
                .stream(integersArray)
                .map(i -> Math.abs(i - average))
                .sum();
        bh.consume(result);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MyBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Setup
    public void setup() throws Exception {
        integersArray = new Random().ints(1, 5).limit((long) 1e7).toArray();
    }
}
