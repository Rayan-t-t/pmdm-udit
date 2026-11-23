import 'package:flutter/material.dart';

import '../data/contenidos.dart';
import '../models/contenido.dart';
import '../widgets/udit_image.dart';
import 'arcade_screen.dart';
import 'ficha_screen.dart';

class CatalogoScreen extends StatefulWidget {
  const CatalogoScreen({super.key});

  @override
  State<CatalogoScreen> createState() => _CatalogoScreenState();
}

class _CatalogoScreenState extends State<CatalogoScreen> {
  final PageController _destacadosController = PageController();
  int _destacadoActual = 0;
  int _paginaActual = 0;

  @override
  void dispose() {
    _destacadosController.dispose();
    super.dispose();
  }

  void _abrirFicha(BuildContext context, Contenido contenido) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(
        builder: (_) => FichaScreen(contenido: contenido),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(78),
        child: _buildHeader(context),
      ),
      body: _paginaActual == 0
          ? _buildInicio(context)
          : const ArcadeScreen(embedded: true),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _paginaActual,
        onDestinationSelected: (index) => setState(() => _paginaActual = index),
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: 'Inicio',
          ),
          NavigationDestination(
            icon: Icon(Icons.sports_esports_outlined),
            selectedIcon: Icon(Icons.sports_esports),
            label: 'Arcade',
            tooltip: 'UDIT Arcade',
          ),
        ],
      ),
    );
  }

  Widget _buildInicio(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 24, 20, 0),
      children: [
        _buildDestacados(context),
        const SizedBox(height: 32),
        _buildCategoria(context, 'Vídeos', const {'video', 'musica'}),
        const SizedBox(height: 28),
        _buildCategoria(context, 'Imágenes', const {'comic'}),
        const SizedBox(height: 28),
        _buildCategoria(context, 'Videojuegos', const {'videojuego'}),
        const SizedBox(height: 32),
        _buildArcadeSection(context),
        const SizedBox(height: 24),
        _buildFooter(),
      ],
    );
  }

  Widget _buildHeader(BuildContext context) {
    return SafeArea(
      bottom: false,
      child: Container(
        decoration: const BoxDecoration(
          color: Colors.white,
          border: Border(bottom: BorderSide(color: Color(0xFFE6EAEE))),
        ),
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'UDITflix',
              style: TextStyle(
                color: Color(0xFF202A33),
                fontSize: 23,
                fontWeight: FontWeight.w700,
                letterSpacing: -0.5,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDestacados(BuildContext context) {
    final destacados = contenidos.where((c) => c.destacado).toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildSectionTitle('Destacados', 'Contenido recomendado'),
        const SizedBox(height: 14),
        SizedBox(
          height: 330,
          child: PageView.builder(
            controller: _destacadosController,
            itemCount: destacados.length,
            onPageChanged: (index) => setState(() => _destacadoActual = index),
            itemBuilder: (context, index) =>
                _buildDestacadoCard(context, destacados[index]),
          ),
        ),
        const SizedBox(height: 12),
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(
            destacados.length,
            (index) => AnimatedContainer(
              duration: const Duration(milliseconds: 180),
              width: index == _destacadoActual ? 20 : 6,
              height: 6,
              margin: const EdgeInsets.symmetric(horizontal: 3),
              decoration: BoxDecoration(
                color: index == _destacadoActual
                    ? Theme.of(context).colorScheme.primary
                    : const Color(0xFFD4DCE2),
                borderRadius: BorderRadius.circular(8),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildDestacadoCard(BuildContext context, Contenido contenido) {
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: () => _abrirFicha(context, contenido),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            SizedBox(height: 132, child: UditImage(url: contenido.imagenUrl)),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 12, 16, 12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      contenido.titulo,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.w700,
                        color: const Color(0xFF202A33),
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      contenido.descripcion,
                      maxLines: 3,
                      overflow: TextOverflow.ellipsis,
                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: const Color(0xFF586570),
                        height: 1.35,
                      ),
                    ),
                    const Spacer(),
                    Row(
                      children: [
                        _MetadataLabel(text: contenido.tipo),
                        const SizedBox(width: 8),
                        _MetadataLabel(text: contenido.duracion),
                        const Spacer(),
                        Icon(
                          Icons.arrow_forward,
                          size: 18,
                          color: Theme.of(context).colorScheme.primary,
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCategoria(
    BuildContext context,
    String titulo,
    Set<String> tipos,
  ) {
    final items = contenidos
        .where((contenido) => tipos.contains(contenido.tipo.toLowerCase()))
        .toList();
    if (items.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildSectionTitle(titulo, '${items.length} contenidos'),
        const SizedBox(height: 14),
        SizedBox(
          height: 270,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: items.length,
            separatorBuilder: (_, _) => const SizedBox(width: 12),
            itemBuilder: (context, index) {
              final contenido = items[index];
              return SizedBox(
                width: 164,
                child: Card(
                  clipBehavior: Clip.antiAlias,
                  child: InkWell(
                    onTap: () => _abrirFicha(context, contenido),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        SizedBox(
                          height: 184,
                          child: UditImage(url: contenido.imagenUrl),
                        ),
                        Padding(
                          padding: const EdgeInsets.fromLTRB(10, 10, 10, 8),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                contenido.titulo,
                                maxLines: 2,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontWeight: FontWeight.w600,
                                  color: Color(0xFF202A33),
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                '${contenido.tipo} · ${contenido.duracion}',
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: Theme.of(context).textTheme.bodySmall
                                    ?.copyWith(color: const Color(0xFF697680)),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _buildArcadeSection(BuildContext context) {
    return Card(
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: Icon(
          Icons.sports_esports_outlined,
          color: Theme.of(context).colorScheme.primary,
        ),
        title: const Text(
          'UDIT Arcade',
          style: TextStyle(
            color: Color(0xFF202A33),
            fontWeight: FontWeight.w600,
          ),
        ),
        subtitle: const Text('Juega a Udit Flappy'),
        trailing: const Icon(Icons.arrow_forward),
        onTap: () => setState(() => _paginaActual = 1),
      ),
    );
  }

  Widget _buildSectionTitle(String title, String subtitle) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(
            color: Color(0xFF202A33),
            fontSize: 20,
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 3),
        Text(
          subtitle,
          style: const TextStyle(color: Color(0xFF697680), fontSize: 13),
        ),
      ],
    );
  }

  Widget _buildFooter() {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(vertical: 20),
      decoration: const BoxDecoration(
        border: Border(top: BorderSide(color: Color(0xFFE1E6EA))),
      ),
      child: const Column(
        children: [
          Text(
            'UDITflix',
            style: TextStyle(
              color: Color(0xFF38566B),
              fontWeight: FontWeight.w700,
              letterSpacing: 0.3,
            ),
          ),
          SizedBox(height: 5),
          Text(
            'Tu espacio de entretenimiento digital',
            style: TextStyle(color: Color(0xFF697680), fontSize: 12),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }
}

class _MetadataLabel extends StatelessWidget {
  const _MetadataLabel({required this.text});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: const Color(0xFFF0F3F5),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        text,
        style: const TextStyle(
          color: Color(0xFF586570),
          fontSize: 11,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}
