class CoreInfo {
  const CoreInfo({
    required this.name,
    required this.installed,
    this.version,
    this.updateAvailable = false,
    this.downloading = false,
  });

  factory CoreInfo.fromMap(Map<dynamic, dynamic> map) {
    return CoreInfo(
      name: (map['name'] ?? '').toString(),
      installed: map['installed'] == true,
      version: map['version']?.toString(),
      updateAvailable: map['updateAvailable'] == true,
      downloading: map['downloading'] == true,
    );
  }

  final String name;
  final bool installed;
  final String? version;
  final bool updateAvailable;
  final bool downloading;

  CoreInfo copyWith({
    String? name,
    bool? installed,
    String? version,
    bool? updateAvailable,
    bool? downloading,
    bool clearVersion = false,
  }) {
    return CoreInfo(
      name: name ?? this.name,
      installed: installed ?? this.installed,
      version: clearVersion ? null : (version ?? this.version),
      updateAvailable: updateAvailable ?? this.updateAvailable,
      downloading: downloading ?? this.downloading,
    );
  }
}
