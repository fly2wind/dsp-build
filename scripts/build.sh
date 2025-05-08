#!/bin/bash

#
# Copyright 2022 [name of copyright owner]
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

current_path=$(
  cd $(dirname $0)
  pwd
)

to_camel_case() {
  IFS=-_ read -ra str <<<"$1"
  printf '%s' "${str[@]^}"
}

# 通过构建
echo $(cat ${current_path}/env.properties | xargs)
