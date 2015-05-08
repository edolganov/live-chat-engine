/*
 * Copyright 2015 Evgeny Dolganov (evgenij.dolganov@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package och.util.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureWrapper implements Future<Void>{
	
	final Future<?> real;

	public FutureWrapper(Future<?> real) {
		super();
		this.real = real;
	}


	@Override
	public boolean isDone() {
		return real.isDone();
	}
	

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return real.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return real.isCancelled();
	}

	@Override
	public Void get() throws InterruptedException, ExecutionException {
		real.get();
		return null;
	}

	@Override
	public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		real.get(timeout, unit);
		return null;
	}

}
