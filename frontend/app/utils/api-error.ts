import type { Message, ProblemDetail } from "./types";

const UNKNOWN_ERROR = "errors.unknown";

export class ApiError extends Error {
  readonly status: number;
  readonly problem: ProblemDetail | null;

  constructor(status: number, problem: ProblemDetail | null) {
    super(problem?.code ?? `errors.status.${status}`);
    this.name = "ApiError";
    this.status = status;
    this.problem = problem;
  }

  get summary(): Message {
    return { code: this.message, args: this.problem?.args ?? {} };
  }

  field(name: string): Message | undefined {
    return this.problem?.errors?.[name];
  }
}

export function messageOf(error: unknown): Message {
  return error instanceof ApiError ? error.summary : { code: UNKNOWN_ERROR, args: {} };
}
