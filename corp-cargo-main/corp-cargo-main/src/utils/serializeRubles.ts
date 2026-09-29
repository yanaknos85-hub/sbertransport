import { formatRubles } from './formatRubles';
import { convertToRubles } from './convertToRubles';

export const serializeRubles = (sum: number | undefined | null) => formatRubles(sum ? convertToRubles(sum) : sum);
