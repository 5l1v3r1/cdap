/*
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

const request = require('request');

function getGETRequestOptions() {
  return {
    method: 'GET',
    json: true,
  };
}

function getPOSTRequestOptions() {
  return {
    method: 'POST',
    json: true,
  };
}

function requestPromiseWrapper(options, token, bodyModifiersFn) {
  if (token) {
    options.headers = {
      Authorization: token,
    };
  }

  return new Promise((resolve, reject) => {
    request(options, (err, response, body) => {
      if (err) {
        return reject(err);
      }

      const statusCode = response.statusCode;

      if (typeof statusCode === 'undefined' || statusCode != 200) {
        const error = {
          statusCode: statusCode,
          body: body,
        };

        return reject(error);
      }

      let resultBody = body;
      if (typeof bodyModifiersFn === 'function') {
        resultBody = bodyModifiersFn(body);
      }

      return resolve(resultBody);
    });
  });
}

module.exports = {
  getGETRequestOptions,
  getPOSTRequestOptions,
  requestPromiseWrapper,
};
