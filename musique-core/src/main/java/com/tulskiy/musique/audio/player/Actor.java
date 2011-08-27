/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.audio.player;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Author: Denis Tulskiy
 * Date: 1/15/11
 */
public abstract class Actor {
    private Logger logger = Logger.getLogger(getClass().getName());

    public enum Message {
        // player messages
        PLAY, PAUSE, STOP, FLUSH,
        // buffer messages
        OPEN, SEEK;

        private Object[] params;

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    private BlockingQueue<Message> queue = new LinkedBlockingDeque<Message>();

    public synchronized void send(Message message, Object... params) {
        message.setParams(params);
        queue.add(message);
    }

    protected Actor() {
        Thread messageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                    while (true) {
                        Message message = null;
                        try {
                            message = queue.take();
                            process(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        } catch (Exception e) {
                            logger.log(Level.WARNING, "Error processing message " + message, e);
                        }
                    }
            }
        }, "Actor Thread");
        messageThread.start();
    }

    protected abstract void process(Message message);
}
