/**
 *  Copyright Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.sf.ehcache.statisticsV2;

import java.util.ArrayList;

import net.sf.ehcache.statisticsV2.Constants.RecordingCost;
import net.sf.ehcache.statisticsV2.Constants.RetrievalCost;

public class EhcacheOperationStatisticBuilder {

    private final RetrievalCost retrievalCost=RetrievalCost.UNKNOWN;
    private final RecordingCost recordingCost=RecordingCost.UNKNOWN;
    private final ArrayList<String> tags=new ArrayList<String>();
    private final String name=null;

    private EhcacheOperationStatisticBuilder() {

    }

    public EhcacheOperationStatisticBuilder start() {
        return new EhcacheOperationStatisticBuilder();
    }


}