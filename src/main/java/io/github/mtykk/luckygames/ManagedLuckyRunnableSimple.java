package io.github.mtykk.luckygames;

import javax.annotation.Nullable;

public class ManagedLuckyRunnableSimple{
    @Nullable
    LuckyRunnableSimple onStart;
    LuckyRunnableSimple luckyRunnableSimple;
    @Nullable
    LuckyRunnableSimple onFinish;
    int minTimes; //Minimum time the event will run
    int maxTimes; //Maximum

    ManagedLuckyRunnableSimple(LuckyRunnableSimple _luckyRunnableSimple,
                               @Nullable LuckyRunnableSimple _onStart,
                               @Nullable LuckyRunnableSimple _onFinish,
                               int _minTimes,
                               int _maxTimes) throws IllegalArgumentException{
        if(_minTimes <= 0 || _maxTimes < _minTimes) throw new IllegalArgumentException();
        this.onStart = _onStart;
        this.onFinish = _onFinish;
        this.luckyRunnableSimple = _luckyRunnableSimple;
        this.minTimes = _minTimes;
        this.maxTimes = _maxTimes;
    }
}