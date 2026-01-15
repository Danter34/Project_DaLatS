using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SafeDalat_API.Migrations
{
    /// <inheritdoc />
    public partial class udqa : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "ResponsibleDepartmentId",
                table: "QuestionCategories",
                type: "int",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_QuestionCategories_ResponsibleDepartmentId",
                table: "QuestionCategories",
                column: "ResponsibleDepartmentId");

            migrationBuilder.AddForeignKey(
                name: "FK_QuestionCategories_Departments_ResponsibleDepartmentId",
                table: "QuestionCategories",
                column: "ResponsibleDepartmentId",
                principalTable: "Departments",
                principalColumn: "DepartmentId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_QuestionCategories_Departments_ResponsibleDepartmentId",
                table: "QuestionCategories");

            migrationBuilder.DropIndex(
                name: "IX_QuestionCategories_ResponsibleDepartmentId",
                table: "QuestionCategories");

            migrationBuilder.DropColumn(
                name: "ResponsibleDepartmentId",
                table: "QuestionCategories");
        }
    }
}
