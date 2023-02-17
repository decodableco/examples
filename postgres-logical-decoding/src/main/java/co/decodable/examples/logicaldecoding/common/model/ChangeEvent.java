/*
 *  Copyright 2023 The original authors
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
package co.decodable.examples.logicaldecoding.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

public class ChangeEvent {

    private String op;
    private long ts_ms;
    private JsonNode source;

    @JsonInclude(Include.ALWAYS)
    private JsonNode before;
    private JsonNode after;

    @JsonInclude(Include.NON_NULL)
    private Message message;

    @JsonInclude(Include.NON_NULL)
    private JsonNode transaction;

    @JsonInclude(Include.NON_NULL)
    private JsonNode auditData;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public long getTs_ms() {
        return ts_ms;
    }

    public void setTs_ms(long ts_ms) {
        this.ts_ms = ts_ms;
    }

    public JsonNode getSource() {
        return source;
    }

    public void setSource(JsonNode source) {
        this.source = source;
    }

    public JsonNode getBefore() {
        return before;
    }

    public void setBefore(JsonNode before) {
        this.before = before;
    }

    public JsonNode getAfter() {
        return after;
    }

    public void setAfter(JsonNode after) {
        this.after = after;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public JsonNode getTransaction() {
        return transaction;
    }

    public void setTransaction(JsonNode transaction) {
        this.transaction = transaction;
    }

    public JsonNode getAuditData() {
        return auditData;
    }

    public void setAuditData(JsonNode auditData) {
        this.auditData = auditData;
    }
}
