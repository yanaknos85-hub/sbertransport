export default function* keyGen(): Generator<string, string, unknown> {
  let current = 0;
  while (true) {
    yield (current++).toString();
  }
}
