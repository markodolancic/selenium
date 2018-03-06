﻿// <copyright file="ISpecificationCompliant.cs" company="WebDriver Committers">
// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements. See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership. The SFC licenses this file
// to you under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// </copyright>

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace OpenQA.Selenium.Remote
{
    /// <summary>
    /// Interface used to determine whether an implementing object is compliant with the W3C WebDriver specification.
    /// </summary>
    internal interface ISpecificationCompliant
    {
        /// <summary>
        /// Gets or sets a value indicating whether this set of capabilities is compliant with the W3C WebDriver specification.
        /// </summary>
        bool IsSpecificationCompliant { get; set; }
  }
}
