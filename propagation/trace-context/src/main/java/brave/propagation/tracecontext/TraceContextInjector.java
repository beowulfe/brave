/*
 * Copyright 2013-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package brave.propagation.tracecontext;

import brave.propagation.Propagation.Setter;
import brave.propagation.TraceContext;
import brave.propagation.TraceContext.Injector;
import brave.propagation.tracecontext.TraceContextPropagation.Extra;

import static brave.propagation.tracecontext.TraceparentFormat.writeTraceparentFormat;

final class TraceContextInjector<C, K> implements Injector<C> {
  final TracestateFormat tracestateFormat;
  final Setter<C, K> setter;
  final K traceparentKey, tracestateKey;

  TraceContextInjector(TraceContextPropagation<K> propagation, Setter<C, K> setter) {
    this.tracestateFormat = new TracestateFormat(propagation.stateName);
    this.traceparentKey = propagation.traceparentKey;
    this.tracestateKey = propagation.tracestateKey;
    this.setter = setter;
  }

  @Override public void inject(TraceContext traceContext, C carrier) {
    String thisState = writeTraceparentFormat(traceContext);
    setter.put(carrier, traceparentKey, thisState);

    CharSequence otherState = null;
    for (int i = 0, length = traceContext.extra().size(); i < length; i++) {
      Object next = traceContext.extra().get(i);
      if (next instanceof Extra) {
        otherState = ((Extra) next).otherState;
        break;
      }
    }

    String tracestate = tracestateFormat.write(thisState, otherState);
    setter.put(carrier, tracestateKey, tracestate);
  }
}
