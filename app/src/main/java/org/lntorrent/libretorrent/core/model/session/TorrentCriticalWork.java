

package org.lntorrent.libretorrent.core.model.session;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

class TorrentCriticalWork
{
    public enum Type
    {
        MOVING,
        SAVE_RESUME
    }

    public class State
    {
        public boolean moving;
        public boolean saveResume;
        public long changeTime;

        public State(boolean moving, boolean saveResume, long changeTime)
        {
            this.moving = moving;
            this.saveResume = saveResume;
            this.changeTime = changeTime;
        }

        public boolean isDuringChange()
        {
            return moving || saveResume;
        }
    }

    private ExecutorService exec = Executors.newFixedThreadPool(2);
    private boolean moving;
    private int saveResume;
    private BehaviorSubject<State> stateChangedEvent =
            BehaviorSubject.createDefault(new State(false, false, System.currentTimeMillis()));

    public boolean isMoving()
    {
        return moving;
    }

    public synchronized void setMoving(boolean moving)
    {
        this.moving = moving;
        emitChangedEvent();
    }

    public boolean isSaveResume()
    {
        return saveResume > 0;
    }

    public synchronized void setSaveResume(boolean saveResume)
    {
        if (saveResume)
            ++this.saveResume;
        else
            --this.saveResume;
        emitChangedEvent();
    }

    public Observable<State> observeStateChanging()
    {
        return stateChangedEvent;
    }

    private void emitChangedEvent()
    {
        exec.submit(() -> {
            stateChangedEvent.onNext(
                    new State(moving, saveResume > 0, System.currentTimeMillis()));
        });
    }
}
