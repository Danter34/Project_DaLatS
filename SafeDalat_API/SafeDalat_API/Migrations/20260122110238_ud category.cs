using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace SafeDalat_API.Migrations
{
    /// <inheritdoc />
    public partial class udcategory : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "DefaultDepartmentId",
                table: "IncidentCategories",
                type: "int",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_IncidentCategories_DefaultDepartmentId",
                table: "IncidentCategories",
                column: "DefaultDepartmentId");

            migrationBuilder.AddForeignKey(
                name: "FK_IncidentCategories_Departments_DefaultDepartmentId",
                table: "IncidentCategories",
                column: "DefaultDepartmentId",
                principalTable: "Departments",
                principalColumn: "DepartmentId");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "FK_IncidentCategories_Departments_DefaultDepartmentId",
                table: "IncidentCategories");

            migrationBuilder.DropIndex(
                name: "IX_IncidentCategories_DefaultDepartmentId",
                table: "IncidentCategories");

            migrationBuilder.DropColumn(
                name: "DefaultDepartmentId",
                table: "IncidentCategories");
        }
    }
}
