// Copyright (c) 2014, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

part of dart.developer;

/// A UserTag can be used to group samples in the Observatory profiler.
abstract class UserTag {
  /// The maximum number of UserTag instances that can be created by a program.
  static const MAX_USER_TAGS = 64;

  external factory UserTag(String label);

  /// Label of [this].
  String get label;

  /// Make [this] the current tag for the isolate. Returns the current tag
  /// before setting.
  UserTag makeCurrent();

  /// The default [UserTag] with label 'Default'.
  external static UserTag get defaultTag;
}

/// Returns the current [UserTag] for the isolate.
external UserTag getCurrentTag();

/// Abstract [Metric] class. Metric names must be unique, are hierarchical,
/// and use periods as separators. For example, 'a.b.c'. Uniqueness is only
/// enforced when a Metric is registered. The name of a metric cannot contain
/// the slash ('/') character.
abstract class Metric {
  /// [name] of this metric.
  final String name;

  /// [description] of this metric.
  final String description;

  Metric(this.name, this.description) {
    if ((name == 'vm') || name.contains('/')) {
      throw new ArgumentError('Invalid Metric name.');
    }
  }

  Map _toJSON();
}

/// A measured value with a min and max. Initial value is min. Value will
/// be clamped to the interval [min, max].
class Gauge extends Metric {
  final double min;
  final double max;

  double _value;
  double get value => _value;
  set value(double v) {
    if (v < min) {
      v = min;
    } else if (v > max) {
      v = max;
    }
    _value = v;
  }

  Gauge(String name, String description, this.min, this.max)
      : super(name, description) {
    ArgumentError.checkNotNull(min, 'min');
    ArgumentError.checkNotNull(max, 'max');
    if (!(min < max)) throw new ArgumentError('min must be less than max');
    _value = min;
  }

  Map _toJSON() {
    var map = {
      'type': 'Gauge',
      'id': 'metrics/$name',
      'name': name,
      'description': description,
      'value': value,
      'min': min,
      'max': max