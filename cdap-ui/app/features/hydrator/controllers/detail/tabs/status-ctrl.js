/*
 * Copyright © 2015 Cask Data, Inc.
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

angular.module(PKG.name + '.feature.hydrator')
  .controller('HydratorDetailStatusController', function(DetailRunsStore, GLOBALS) {
    this.setState = function() {
      this.runsCount = DetailRunsStore.getRunsCount();
      var runs = DetailRunsStore.getRuns().list;
      var status, i;
      for (i=0 ; i<runs.length; i++) {
        status = runs[i].status;
        if (['RUNNING', 'STARTING', 'STOPPING'].indexOf(status) === -1) {
          this.lastFinished = runs[i];
          break;
        }
      }
      this.lastRunTime = runs.length > 0 && runs[0].end ? runs[0].end - runs[0].start : 'N/A';
      this.averageRunTime = DetailRunsStore.getStatistics().avgRunTime || 'N/A';
    };
    this.setState();
    this.GLOBALS = GLOBALS;
    this.pipelineType = DetailRunsStore.getPipelineType();
    DetailRunsStore.registerOnChangeListener(this.setState.bind(this));


    // var params = {};
    // angular.copy(HydratorDetail.params, params);
    // params.scope = $scope;
    //
    // $scope.stats = {};
    //
    // $scope.programType = HydratorDetail.programType;
    //
    // HydratorDetail.api.pollRuns(params)
    //   .$promise
    //   .then(function (runs) {
    //     $scope.stats.numRuns = runs.length;
    //     $scope.stats.lastRunTime = runs.length > 0 && runs[0].end ? runs[0].end - runs[0].start : 'N/A';
    //
    //     for (var i = 0; i < runs.length; i++) {
    //       var status = runs[i].status;
    //
    //       if (['RUNNING', 'STARTING', 'STOPPING'].indexOf(status) === -1) {
    //         $scope.stats.lastFinished = runs[i];
    //         break;
    //       }
    //     }
    //   });
    //
    // if (HydratorDetail.programType === 'WORKFLOWS') {
    //   HydratorDetail.api.pollStatistics(params)
    //     .$promise
    //     .then(function (stats) {
    //       $scope.stats.avgRunTime = stats.avgRunTime ? stats.avgRunTime : 'N/A';
    //     });
    // }
  });
