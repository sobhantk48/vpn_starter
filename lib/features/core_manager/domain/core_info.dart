class CoreInfo {
  const CoreInfo({
    required this.name,
    required this.installed,
    this.version,
  });

  final String name;
  final bool installed;
  final String? version;

  factory CoreInfo.fromMap(Map<dynamic, dynamic> map) {
    return CoreInfo(
      name: (map['name'] ?? '').toString(),
      installed: map['installed'] == true,
      version: map['version']?.toString(),
    );
  }

  CoreInfo copyWith({
    String? name,
    bool? installed,
    String? version,
    bool clearVersion = false,
  }) {
    return CoreInfo(
      name: name ?? this.name,
      installed: installed ?? this.installed,
      version: clearVersion ? null : (version ?? this.version),
    );
  }
}
