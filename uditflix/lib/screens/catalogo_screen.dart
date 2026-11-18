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

  void _abrirArcade(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute<void>(builder: (_) => const ArcadeScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(112),
        child: _buildHeader(context),
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(20, 24, 20, 0),
        children: [
          _buildDestacados(context),
          const SizedBox(height: 32),
          _buildCatalogo(context),
          const SizedBox(height: 36),
          _buildFooter(),
        ],
      ),
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
            const SizedBox(height: 8),
            Row(
              children: [
                _HeaderButton(
                  label: 'Inicio',
                  icon: Icons.home_outlined,
                  selected: true,
                  onPressed: () {},
                ),
                const SizedBox(width: 8),
                _HeaderButton(
                  label: 'Arcade',
                  icon: Icons.sports_esports_outlined,
                  tooltip: 'UDIT Arcade',
                  onPressed: () => _abrirArcade(context),
                ),
              ],
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
            SizedBox(
              height: 132,
              child: UditImage(url: contenido.imagenUrl),
            ),
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

  Widget _buildCatalogo(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _buildSectionTitle('Explorar catálogo', 'Vídeos, cómics y más'),
        const SizedBox(height: 14),
        SizedBox(
          height: 270,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: contenidos.length,
            separatorBuilder: (_, _) => const SizedBox(width: 12),
            itemBuilder: (context, index) {
              final contenido = contenidos[index];
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

class _HeaderButton extends StatelessWidget {
  const _HeaderButton({
    required this.label,
    required this.icon,
    required this.onPressed,
    this.selected = false,
    this.tooltip,
  });

  final String label;
  final IconData icon;
  final VoidCallback onPressed;
  final bool selected;
  final String? tooltip;

  @override
  Widget build(BuildContext context) {
    final color = selected
        ? Theme.of(context).colorScheme.primary
        : const Color(0xFF586570);
    final button = TextButton.icon(
      onPressed: onPressed,
      icon: Icon(icon, size: 18),
      label: Text(label),
      style: TextButton.styleFrom(
        foregroundColor: color,
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        textStyle: TextStyle(
          fontWeight: selected ? FontWeight.w600 : FontWeight.w500,
        ),
      ),
    );
    return tooltip == null ? button : Tooltip(message: tooltip!, child: button);
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
