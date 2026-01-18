export interface AnswerDTO {
  answerId: number;
  content: string;
  createdAt: string;
  responderId: number;
  responderName: string;
  departmentName: string;
}

export interface QuestionDTO {
  questionId: number;
  content: string;
  createdAt: string;
  userId: number;
  userName: string;
  questionCategoryName: string;
  assignedDepartmentName?: string;
  answers: AnswerDTO[];
}

export interface CreateAnswerDTO {
  content: string;
}