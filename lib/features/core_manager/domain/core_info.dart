import 'package:equatable/equatable.dart';

class CoreInfo extends Equatable {
  final String name;
  final String? version;
  final bool installed;
  final bool updateAvailable;
  final bool downloading;

  const CoreInfo({
    required this.name,
    this.version,
    required this.installed,
    required this.updateAvailable,
    required this.downloading,
  });

  CoreInfo copyWith({
    String? name,
    String? version,
    bool? installed,
    bool? updateAvailable,
    bool? downloading,
  }) {
    return CoreInfo(
      name: name ?? this.name,
      version: version ?? this.version,
      installed: installed ?? this.installed,
      updateAvailable: updateAvailable ?? this.updateAvailable,
      downloading: downloading ?? this.downloading,
    );
  }

  @override
  List<Object?> get props =>
      [name, version, installed, updateAvailable, downloading];
}
