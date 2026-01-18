import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { QaService } from '../../services/qa.service';
import { AuthService } from '../../services/auth.service';
import { AnswerDTO, QuestionDTO } from '../../models/qa.model';

@Component({
  selector: 'app-qa',
  templateUrl: './qa.component.html',
  styleUrls: ['./qa.component.css'],
  standalone: false
})
export class QaComponent implements OnInit {

  // Dữ liệu
  allQuestions: QuestionDTO[] = [];
  filteredQuestions: QuestionDTO[] = [];
  selectedQuestion: QuestionDTO | null = null;

  // Bộ lọc: 'all' hoặc 'unanswered'
  filterMode: 'all' | 'unanswered' = 'all';

  // Form trả lời
  replyContent: string = '';
  isSending: boolean = false;
  currentUser: any = null;

  constructor(
    private qaService: QaService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.loadData();
  }

  loadData() {
    this.qaService.getAllQuestions().subscribe({
      next: (res) => {
        this.allQuestions = res;
        this.applyFilter();
        this.cdr.detectChanges();
      },
      error: (err) => console.error(err)
    });
  }

  // --- 1. LOGIC XỬ LÝ TÊN HIỂN THỊ (Yêu cầu của bạn) ---
  getResponderName(ans: AnswerDTO): string {
    // Nếu là Admin (dựa vào tên phòng ban hoặc tên người trả lời)
    if (ans.departmentName === 'Trung tâm điều hành thông minh (IOC)' || ans.responderName.toLowerCase() === 'Admin') {
      return 'Quản trị viên';
    }

    // Nếu là Staff: "Cán bộ + [Tên danh mục]"
    // Lấy tên danh mục từ câu hỏi đang được chọn
    if (this.selectedQuestion) {
      return `Cán bộ ${this.selectedQuestion.questionCategoryName}`;
    }

    return 'Cán bộ chuyên môn';
  }

  // --- 2. LOGIC LẤY CHỮ CÁI ĐẦU CHO AVATAR ---
  getAvatarFromAnswer(ans: AnswerDTO): string {
    // Lấy cái tên đã được xử lý ở hàm trên (Ví dụ: "Cán bộ..." -> lấy chữ C)
    const displayName = this.getResponderName(ans);
    return displayName ? displayName.charAt(0).toUpperCase() : 'A';
  }

  // --- CÁC LOGIC KHÁC ---

  setFilter(mode: 'all' | 'unanswered') {
    this.filterMode = mode;
    this.applyFilter();
    this.selectedQuestion = null; // Reset selection khi đổi tab
  }

  applyFilter() {
    if (this.filterMode === 'unanswered') {
      this.filteredQuestions = this.allQuestions.filter(q => q.answers.length === 0);
    } else {
      this.filteredQuestions = [...this.allQuestions];
    }
  }

  selectQuestion(q: QuestionDTO) {
    this.selectedQuestion = q;
    this.replyContent = '';
    // Cuộn xuống cuối sau khi view render xong
    setTimeout(() => this.scrollToBottom(), 100);
  }

  sendReply() {
    if (!this.selectedQuestion || !this.replyContent.trim()) return;

    this.isSending = true;
    const qId = this.selectedQuestion.questionId;

    this.qaService.sendAnswer(qId, this.replyContent).subscribe({
      next: () => {
        // Tạo câu trả lời giả lập để hiện ngay lập tức
        const newAnswer: AnswerDTO = {
          answerId: 0,
          content: this.replyContent,
          createdAt: new Date().toISOString(),
          responderId: this.currentUser?.userId || 0,
          responderName: this.currentUser?.fullName || 'Admin', 
          departmentName: this.currentUser?.role === 'Admin' ? 'Quản trị viên' : 'Staff' // Tạm thời
        };

        if (this.selectedQuestion) {
          this.selectedQuestion.answers.push(newAnswer);
        }

        this.replyContent = '';
        this.isSending = false;
        this.scrollToBottom();
        this.cdr.detectChanges();
      },
      error: () => {
        alert('Lỗi gửi tin nhắn');
        this.isSending = false;
      }
    });
  }

  scrollToBottom() {
    const el = document.getElementById('chatHistory');
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }

  isUnanswered(q: QuestionDTO): boolean {
    return q.answers.length === 0;
  }
}