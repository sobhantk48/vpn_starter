class Profile {
  const Profile({
    required this.id,
    required this.name,
    required this.coreName,
    this.isActive = false,
  });

  final String id;
  final String name;
  final String coreName;
  final bool isActive;

  factory Profile.defaultProfile() {
    return const Profile(
      id: 'default',
      name: 'Default Profile',
      coreName: 'sing-box',
      isActive: true,
    );
  }

  Profile copyWith({
    String? id,
    String? name,
    String? coreName,
    bool? isActive,
  }) {
    return Profile(
      id: id ?? this.id,
      name: name ?? this.name,
      coreName: coreName ?? this.coreName,
      isActive: isActive ?? this.isActive,
    );
  }
}
